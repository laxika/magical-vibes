package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldOfAmity.class, HolyStrength.class, GrizzlyBears.class})
class HeraldOfAmityTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only an Aura among the top eight cards")
    void entersAndOffersOnlyAuraFromTopEight() {
        HolyStrength aura = new HolyStrength();
        List<Card> library = List.of(
                aura,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        castHerald(library, 4);

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactly(aura.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    @DisplayName("Casts the chosen Aura for free and puts the rest on the library bottom")
    void castsAuraForFreeAndBottomsTheRest() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength aura = new HolyStrength();
        List<Card> library = List.of(
                aura,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        castHerald(library, 5);

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(library.subList(1, library.size()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == aura
                        && target.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Puts all eight cards on the library bottom when no Aura is found")
    void noAuraFinishesWithoutCastChoice() {
        List<Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        castHerald(library, 4);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    @DisplayName("Attack boost counts only Auras controlled by Herald's controller and expires")
    void attackBoostCountsControlledAurasAndExpires() {
        Permanent herald = addCreatureReady(player1, new HeraldOfAmity());
        addAttachedAura(player1, herald);

        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addAttachedAura(player2, opponentCreature);
        addAttachedAura(player2, opponentCreature);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(herald)));
        resolveAllTriggers();

        assertThat(herald.getPowerModifier()).isEqualTo(1);
        assertThat(herald.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(herald.getPowerModifier()).isZero();
        assertThat(herald.getToughnessModifier()).isZero();
    }

    private void castHerald(List<Card> library, int whiteMana) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new HeraldOfAmity()));
        harness.addMana(player1, ManaColor.WHITE, whiteMana);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addAttachedAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
