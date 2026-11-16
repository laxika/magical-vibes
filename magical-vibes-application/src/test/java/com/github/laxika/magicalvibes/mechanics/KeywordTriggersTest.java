package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.a.Acridian;
import com.github.laxika.magicalvibes.cards.a.AmbitiousAugmenter;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    /** An enchantment whose static ability gives the other creatures its controller controls {@code keyword}. */
    private static Card grantsToOtherCreaturesYouControl(Keyword keyword) {
        Card card = new Card();
        card.setName("Anthem");
        card.setType(CardType.ENCHANTMENT);
        card.addStaticEffect(new GrantKeywordEffect(keyword, GrantScope.OWN_CREATURES));
        return card;
    }
}
