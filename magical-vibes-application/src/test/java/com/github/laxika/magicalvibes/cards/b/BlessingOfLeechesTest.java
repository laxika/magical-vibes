package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlessingOfLeeches.class, BileUrchin.class, BakuAltar.class})
class BlessingOfLeechesTest extends BaseCardTest {

    // "Flash / Enchant creature / At the beginning of your upkeep, you lose 1 life. / {0}: Regenerate enchanted creature."

    /** Puts a Blessing of Leeches on player1's battlefield attached to a creature, so it survives state-based checks. */
    private Permanent attachedBlessing() {
        Permanent creature = addCreatureReady(player1, new BileUrchin());
        return attachBlessing(player1, creature);
    }

    private Permanent attachBlessing(Player auraController, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new BlessingOfLeeches());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Resolving Blessing of Leeches attaches it to the target creature")
    void resolvesAndAttaches() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BileUrchin());
        harness.setHand(player1, List.of(new BlessingOfLeeches()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Blessing of Leeches")
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BileUrchin());
        harness.setHand(player1, List.of(new BlessingOfLeeches()));
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.ensurePriority(player1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Blessing of Leeches").getAttachedTo())
                .isEqualTo(creature.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Upkeep life loss belongs to the Aura controller, not the enchanted creature's controller")
    void auraControllerLosesLifeWhenEnchantingOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new BileUrchin());
        attachBlessing(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A regeneration shield does not prevent sacrificing the enchanted creature")
    void regenerationDoesNotPreventSacrifice() {
        Permanent creature = addCreatureReady(player1, new BileUrchin());
        Permanent aura = attachBlessing(player1, creature);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature),
                null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bile Urchin");
        harness.assertInGraveyard(player1, "Bile Urchin");
        harness.assertNotOnBattlefield(player1, "Blessing of Leeches");
        harness.assertInGraveyard(player1, "Blessing of Leeches");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Controller loses 1 life at the beginning of their upkeep")
    void losesLifeAtUpkeep() {
        attachedBlessing();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        attachedBlessing();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The free ability grants a regeneration shield to the enchanted creature")
    void abilityGrantsShieldToEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new BileUrchin());
        Permanent aura = attachBlessing(player1, creature);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        assertThat(aura.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("The free ability can regenerate an opponent's enchanted creature")
    void abilityGrantsShieldToOpponentsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new BileUrchin());
        Permanent aura = attachBlessing(player1, creature);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantANoncreaturePermanent() {
        harness.addToBattlefield(player2, new BileUrchin());
        harness.addToBattlefield(player1, new BakuAltar());
        harness.setHand(player1, List.of(new BlessingOfLeeches()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        Permanent altar = findPermanent(player1, "Baku Altar");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, altar.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
