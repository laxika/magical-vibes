package com.github.laxika.magicalvibes.cards.g;

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
        WrathOfGod.class, SwordsToPlowshares.class})
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
        harness.castInstant(player2, 0, oketraPermanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), oketra.getId(), third.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(oketra.getId()));
    }

    private void destroyOketraWithWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
