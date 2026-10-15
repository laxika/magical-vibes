package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QuickStudy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SplashySpellcaster.class, GrizzlyBears.class, Shock.class,
        Gingerbrute.class, QuickStudy.class, SleightOfHand.class})
class SplashySpellcasterTest extends BaseCardTest {

    @Test
    void castingAnInstantCreatesSorcererRoleAttachedToAnotherCreatureYouControl() {
        addCreatureReady(player1, new SplashySpellcaster());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Sorcerer");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void castingACreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SplashySpellcaster());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    void canChooseNoTarget() {
        harness.addToBattlefield(player1, new SplashySpellcaster());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Sorcerer")).isZero();
    }

    @Test
    void sorceryCreatesRoleBeforeTheSpellResolves() {
        harness.addToBattlefield(player1, new SplashySpellcaster());
        Permanent target = addCreatureReady(player1, new Gingerbrute());
        Card top = new Gingerbrute();
        Card second = new QuickStudy();
        harness.setLibrary(player1, List.of(top, second));

        harness.castFromHand(player1, new SleightOfHand(), "{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sorcerer").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top.getId()));
        harness.assertInGraveyard(player1, "Sleight of Hand");
    }

    @Test
    void targetChoiceExcludesSourceAndOpposingCreaturesAndCanBeDeclined() {
        Permanent source = addCreatureReady(player1, new SplashySpellcaster());
        Permanent ally = addCreatureReady(player1, new Gingerbrute());
        Permanent opponent = addCreatureReady(player2, new Gingerbrute());

        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ally.getId(), player1.getId())
                .doesNotContain(source.getId(), opponent.getId());

        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Sorcerer")).isZero();
    }

    @Test
    void opponentCastingAnInstantDoesNotTrigger() {
        harness.addToBattlefield(player1, new SplashySpellcaster());
        harness.addToBattlefield(player1, new Gingerbrute());

        harness.castFromHand(player2, new QuickStudy(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Sorcerer")).isZero();
    }

    @Test
    void enchantedCreatureAttackingScriesOne() {
        harness.addToBattlefield(player1, new SplashySpellcaster());
        Permanent target = addCreatureReady(player1, new Gingerbrute());
        createRoleWithQuickStudy(target);
        Card top = new Gingerbrute();
        Card second = new QuickStudy();
        harness.setLibrary(player1, List.of(top, second));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
    }

    @Test
    void anotherRoleReplacesTheOldRoleInsteadOfStackingBonuses() {
        harness.addToBattlefield(player1, new SplashySpellcaster());
        Permanent target = addCreatureReady(player1, new Gingerbrute());
        createRoleWithQuickStudy(target);
        Permanent oldRole = findPermanent(player1, "Sorcerer");

        createRoleWithQuickStudy(target);

        assertThat(countPermanents(player1, "Sorcerer")).isEqualTo(1);
        assertThat(findPermanent(player1, "Sorcerer").getId()).isNotEqualTo(oldRole.getId());
        assertThat(findPermanent(player1, "Sorcerer").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void targetLeavingBeforeResolutionPreventsTokenCreation() {
        harness.addToBattlefield(player1, new SplashySpellcaster());
        Permanent target = addCreatureReady(player1, new Gingerbrute());
        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gingerbrute");
        assertThat(countPermanents(player1, "Sorcerer")).isZero();
        harness.assertLife(player1, 23);
    }

    private void createRoleWithQuickStudy(Permanent target) {
        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
    }
}
