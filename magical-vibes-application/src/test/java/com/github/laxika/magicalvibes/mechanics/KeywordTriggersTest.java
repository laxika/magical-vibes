package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.a.Acridian;
import com.github.laxika.magicalvibes.cards.a.AmbitiousAugmenter;
import com.github.laxika.magicalvibes.cards.c.CacklingCounterpart;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The triggered abilities keywords stand for are listed with a permanent's other triggered abilities, once for each
 * instance of the keyword it has, printed or granted, each triggering separately and judging its intervening "if" as
 * it triggers and again as it resolves (CR 603.4).
 */
class KeywordTriggersTest extends BaseCardTest {

    @Test
    @DisplayName("A creature with printed and granted increment triggers twice, each instance checking its \"if\" "
            + "again as it resolves (CR 702.191b)")
    void eachIncrementInstanceTriggersSeparately() {
        Permanent augmenter = harness.addToBattlefieldAndReturn(player1, new AmbitiousAugmenter());
        harness.addToBattlefield(player1, grantsToOtherCreaturesYouControl(Keyword.INCREMENT));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack.stream().filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY))
                .hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        // Four mana is greater than the 1/1's power, then than the 2/2's.
        assertThat(augmenter.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Echo triggers for a permanent that entered without its abilities if it has echo as the upkeep begins "
            + "(CR 702.30a)")
    void echoTriggersForPermanentThatEnteredWithoutItsAbilities() {
        Permanent humility = harness.addToBattlefieldAndReturn(player2, new Humility());
        harness.castFromHand(player1, new Acridian(), "{1}{G}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Acridian");
        gd.playerBattlefields.get(player2.getId()).remove(humility);
        giveManaSource(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Echo doesn't trigger for a permanent its controller has controlled since before their last upkeep")
    void echoDoesNotTriggerAfterAnUpkeepUnderTheSameController() {
        Permanent acridian = harness.addToBattlefieldAndReturn(player1, new Acridian());
        acridian.markControllerUpkeepStarted();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Echo is put on the stack by the trigger pipeline as the permanent's own upkeep ability, with its cost")
    void echoIsThePermanentsOwnUpkeepAbility() {
        Permanent acridian = harness.addToBattlefieldAndReturn(player1, new Acridian());

        advanceToUpkeep(player1);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getSourcePermanentId()).isEqualTo(acridian.getId());
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        });
    }

    @Test
    @DisplayName("Echo triggers at the upkeep of a player who gained control of the permanent, who may then pay it "
            + "(CR 702.30a)")
    void echoTriggersForTheNewControllerAfterAControlChange() {
        Permanent acridian = harness.addToBattlefieldAndReturn(player2, new Acridian());
        acridian.markControllerUpkeepStarted();
        harness.setHand(player1, List.of(new Persuasion()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, acridian.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Acridian");
        giveManaSource(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.assertNotOnBattlefield(player1, "Acridian");
        harness.assertInGraveyard(player2, "Acridian");
    }

    @Test
    @DisplayName("A token copy of an echo permanent has echo and triggers at its controller's next upkeep, the original "
            + "it copied doesn't")
    void tokenCopyOfAnEchoPermanentHasEcho() {
        Permanent acridian = harness.addToBattlefieldAndReturn(player1, new Acridian());
        acridian.markControllerUpkeepStarted();
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, acridian.getId());
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Acridian").stream()
                .filter(permanent -> permanent != acridian).findFirst().orElseThrow();

        advanceToUpkeep(player1);

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getSourcePermanentId()).isEqualTo(token.getId()));
    }

    @Test
    @DisplayName("A face-down echo card has no echo, so nothing triggers at its controller's upkeep (CR 708.2a)")
    void faceDownEchoCardHasNoEcho() {
        Permanent acridian = harness.addToBattlefieldAndReturn(player1, new Acridian());
        acridian.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Echo whose permanent another player gained control of in response checks its \"if\" again and does "
            + "nothing, asking no one (CR 603.4)")
    void echoChecksItsIfAgainAsItResolves() {
        harness.addToBattlefieldAndReturn(player1, new Acridian());
        giveManaSource(player1);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Acridian"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Acridian");
    }

    @Test
    @DisplayName("Echo whose permanent left the battlefield in response is judged by its last-known information: its "
            + "controller is still asked, and declining sacrifices nothing (CR 603.4, 608.2h)")
    void echoOfAPermanentThatLeftStillAsks() {
        harness.addToBattlefieldAndReturn(player1, new Acridian());
        giveManaSource(player1);
        advanceToUpkeep(player1);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Acridian"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Acridian");
    }

    @Test
    @DisplayName("An echo mana payer with no way at all to make mana isn't asked, and the permanent is sacrificed "
            + "(CR 118.3, 608.2d)")
    void echoPayerWithNoManaSourceIsNotAsked() {
        harness.addToBattlefieldAndReturn(player1, new Acridian());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Acridian");
    }

    @Test
    @DisplayName("Echo asks for its cost in the cost's own words")
    void echoAsksInTheCostsWords() {
        harness.addToBattlefieldAndReturn(player1, new Acridian());
        giveManaSource(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).description())
                .isEqualTo("Pay {1}{G}?");
    }

    /** An enchantment whose static ability gives the other creatures its controller controls {@code keyword}. */
    private static Card grantsToOtherCreaturesYouControl(Keyword keyword) {
        Card card = new Card();
        card.setName("Anthem");
        card.setType(CardType.ENCHANTMENT);
        card.addStaticEffect(new GrantKeywordEffect(keyword, Subjects.otherCreaturesYouControl()));
        return card;
    }
}
