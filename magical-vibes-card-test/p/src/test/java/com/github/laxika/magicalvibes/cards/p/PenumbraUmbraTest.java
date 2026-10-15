package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PenumbraUmbra.class, GrizzlyBears.class, Disenchant.class, DoomBlade.class})
class PenumbraUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a black token copy of the enchanted creature when Penumbra Umbra is destroyed")
    void createsBlackCopyWhenAuraIsDestroyed() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castAuraOn(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, aura.getId());
        resolveStackFully();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Penumbra Umbra"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(bears)
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.getCard().getColor() == CardColor.BLACK
                        && permanent.getCard().getPower() == 2
                        && permanent.getCard().getToughness() == 2);
    }

    @Test
    @DisplayName("Umbra armor saves the enchanted creature and still creates a black copy")
    void umbraArmorCreatesBlackCopyWhenCreatureWouldBeDestroyed() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castAuraOn(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, bears.getId());
        resolveStackFully();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Penumbra Umbra"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.getCard().getColor() == CardColor.BLACK);
    }

    @Test
    @DisplayName("Can enchant only a creature controlled by its controller")
    void cannotEnchantOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PenumbraUmbra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copies the creature's last known values if it dies before the Aura trigger resolves")
    void copiesCreatureThatLeavesBeforeTriggerResolves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castAuraOn(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant(), new DoomBlade()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, bears.getId());
        resolveStackFully();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(2);
                });
    }

    private Permanent castAuraOn(Permanent creature) {
        harness.setHand(player1, List.of(new PenumbraUmbra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Penumbra Umbra");
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
