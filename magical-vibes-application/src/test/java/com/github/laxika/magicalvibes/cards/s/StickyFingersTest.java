package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({StickyFingers.class, CivicGardener.class, Murder.class, Mountain.class})
class StickyFingersTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has menace")
    void enchantedCreatureHasMenace() {
        Permanent enchanted = addCreatureReady(player1, new CivicGardener());
        addAura(player1, enchanted);

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature creates a Treasure when it deals combat damage to a player")
    void createsTreasureOnCombatDamageToPlayer() {
        Permanent enchanted = addCreatureReady(player1, new CivicGardener());
        addAura(player1, enchanted);
        enchanted.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE)))
                .hasSize(1);
    }

    @Test
    @DisplayName("Aura controller draws when the enchanted creature dies")
    void auraControllerDrawsWhenEnchantedCreatureDies() {
        Permanent enchanted = addCreatureReady(player1, new CivicGardener());
        addAura(player2, enchanted);
        harness.setLibrary(player2, List.of(new CivicGardener()));

        destroyCreature(player2, enchanted);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sticky Fingers can enchant only a creature")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new CivicGardener());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new StickyFingers()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The enchanted creature's controller creates the Treasure, even with an opposing Aura")
    void creatureControllerCreatesTreasureWithOpposingAura() {
        Permanent enchanted = addCreatureReady(player1, new CivicGardener());
        addAura(player2, enchanted);
        enchanted.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE)))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE)))
                .isEmpty();
    }

    @Test
    @DisplayName("Each Sticky Fingers draws a card when their enchanted creature dies")
    void multipleAurasEachDrawOnDeath() {
        Permanent enchanted = addCreatureReady(player1, new CivicGardener());
        addAura(player2, enchanted);
        addAura(player2, enchanted);
        harness.setLibrary(player2, List.of(new CivicGardener(), new CivicGardener()));

        destroyCreature(player2, enchanted);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An unrelated creature dying does not trigger Sticky Fingers")
    void unrelatedCreatureDeathDoesNotDraw() {
        Permanent enchanted = addCreatureReady(player1, new CivicGardener());
        Permanent other = addCreatureReady(player1, new CivicGardener());
        addAura(player2, enchanted);
        harness.setLibrary(player2, List.of(new CivicGardener()));

        destroyCreature(player2, other);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.MENACE)).isTrue();
    }

    private void addAura(Player controller, Permanent enchanted) {
        Permanent aura = new Permanent(new StickyFingers());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }

    private void destroyCreature(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
