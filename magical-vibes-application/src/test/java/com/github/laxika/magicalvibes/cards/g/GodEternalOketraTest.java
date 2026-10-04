package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GodEternalOketra.class, GrizzlyBears.class, Forest.class, Mountain.class, Plains.class,
        WrathOfGod.class, SwordsToPlowshares.class, Cloudshift.class})
class GodEternalOketraTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature creates a vigilant 4/4 Zombie Warrior token")
    void creatureCastCreatesVigilantZombieWarrior() {
        harness.addToBattlefield(player1, new GodEternalOketra());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Zombie Warrior");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The death trigger may put God-Eternal Oketra third from the top")
    void deathTriggerPutsOketraThirdFromTop() {
        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        harness.addToBattlefield(player1, new GodEternalOketra());
        Card oketra = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();

        destroyOketraWithWrath();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), oketra.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(oketra.getId()));
    }

    @Test
    @DisplayName("Declining the death trigger leaves God-Eternal Oketra in the graveyard")
    void decliningDeathTriggerLeavesOketraInGraveyard() {
        harness.addToBattlefield(player1, new GodEternalOketra());
        Card oketra = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();

        destroyOketraWithWrath();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(oketra.getId()));
    }

    @Test
    @DisplayName("The exile trigger may put God-Eternal Oketra third from the top")
    void exileTriggerPutsOketraThirdFromTop() {
        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        Permanent oketraPermanent = harness.addToBattlefieldAndReturn(player1, new GodEternalOketra());
        Card oketra = oketraPermanent.getCard();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, oketraPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), oketra.getId(), third.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(oketra.getId()));
    }

    @Test
    @DisplayName("Opponent's creature spells do not create tokens")
    void opponentCreatureDoesNotTriggerOketra() {
        harness.addToBattlefield(player1, new GodEternalOketra());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Zombie Warrior");
    }

    @Test
    @DisplayName("Casting Oketra does not trigger its own token ability")
    void castingOketraDoesNotCreateToken() {
        harness.setHand(player1, List.of(new GodEternalOketra()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "God-Eternal Oketra");
        harness.assertNotOnBattlefield(player1, "Zombie Warrior");
    }

    @Test
    @DisplayName("A noncreature spell does not create a token")
    void noncreatureSpellDoesNotTriggerOketra() {
        harness.addToBattlefield(player1, new GodEternalOketra());

        destroyOketraWithWrath();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Zombie Warrior");
    }

    @Test
    @DisplayName("Oketra goes to the bottom when its owner's library has fewer than two cards")
    void deathTriggerWithShortLibrary() {
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top));
        Permanent oketra = harness.addToBattlefieldAndReturn(player1, new GodEternalOketra());

        destroyOketraWithWrath();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), oketra.getCard().getId());
    }

    @Test
    @DisplayName("Declining the exile trigger leaves Oketra in exile")
    void decliningExileTriggerLeavesOketraInExile() {
        Permanent oketra = harness.addToBattlefieldAndReturn(player1, new GodEternalOketra());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, oketra.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(oketra.getCard().getId());
    }

    @Test
    @DisplayName("An older exile trigger cannot move Oketra after it returns and is exiled again")
    void oldExileTriggerCannotMoveNewExileIncarnation() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GodEternalOketra());
        Card oketra = original.getCard();
        harness.setHand(player1, List.of(new Cloudshift(), new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());
        Permanent returned = findPermanent(player1, "God-Eternal Oketra");
        harness.castAndResolveInstant(player1, 0, returned.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(oketra.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(oketra.getId());
    }

    private void destroyOketraWithWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
