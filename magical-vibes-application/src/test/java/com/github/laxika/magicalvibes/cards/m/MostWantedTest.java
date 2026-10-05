package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MostWanted.class, GrizzlyBears.class, DoomBlade.class, Mountain.class})
class MostWantedTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+1")
    void enchantedCreatureGetsBoost() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        addAura(player1, enchanted);

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
    }

    @Test
    @DisplayName("When the enchanted creature dies, creates two Treasures")
    void createsTwoTreasuresWhenEnchantedCreatureDies() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        addAura(player1, enchanted);

        enchanted.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE)))
                .hasSize(2);
    }

    @Test
    @DisplayName("The Aura controller creates the Treasures")
    void auraControllerCreatesTreasures() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        addAura(player2, enchanted);

        destroyCreature(enchanted);

        assertThat(countTreasures(player1)).isZero();
        assertThat(countTreasures(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("Most Wanted can enchant only a creature")
    void cannotEnchantALand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new MostWanted()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Most Wanted can be cast in response to removal on an opponent's turn")
    void canBeCastInResponseOnOpponentsTurn() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, enchanted.getId());

        harness.setHand(player1, List.of(new MostWanted()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Most Wanted").getAttachedTo()).isEqualTo(enchanted.getId());
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchanted);
        assertThat(countTreasures(player1)).isEqualTo(2);
        assertThat(countTreasures(player2)).isZero();
    }

    @Test
    @DisplayName("An unrelated creature dying does not create Treasures")
    void unrelatedCreatureDeathDoesNotCreateTreasures() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent unrelated = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, enchanted);

        destroyCreature(unrelated);

        assertThat(countTreasures(player1)).isZero();
        assertThat(countTreasures(player2)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enchanted);
    }

    private void addAura(Player controller, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new MostWanted());
        aura.setAttachedTo(enchanted.getId());
    }

    private void destroyCreature(Permanent enchanted) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, enchanted.getId());
        resolveAllTriggers();
    }

    private long countTreasures(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .count();
    }
}
