package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HakimLoreweaverTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep ability returns the targeted Aura from the graveyard attached to Hakim")
    void upkeepAbilityReturnsAuraAttachedToHakim() {
        Permanent hakim = addHakim(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, holyStrength.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotInGraveyard(player1, "Holy Strength");
        Permanent aura = findPermanent(player1, "Holy Strength");
        assertThat(aura).isNotNull();
        assertThat(aura.getAttachedTo()).isEqualTo(hakim.getId());
    }

    @Test
    @DisplayName("Upkeep ability can't be activated outside the controller's upkeep")
    void upkeepAbilityRejectedOutsideUpkeep() {
        addHakim(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, holyStrength.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Upkeep ability can't be activated while Hakim is enchanted")
    void upkeepAbilityRejectedWhileEnchanted() {
        Permanent hakim = addHakim(player1);
        attachAura(player1, new Pacifism(), hakim);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, holyStrength.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability destroys only the Auras attached to Hakim")
    void tapAbilityDestroysAurasAttachedToHakim() {
        Permanent hakim = addHakim(player1);
        attachAura(player1, new Pacifism(), hakim);
        attachAura(player1, new HolyStrength(), hakim);
        Permanent bears = addCreature(player1, new GrizzlyBears());
        attachAura(player1, new Pacifism(), bears);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(hakim.isTapped()).isTrue();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Holy Strength"));
        // The Aura on Grizzly Bears is untouched; the one that was on Hakim is in the graveyard.
        Permanent survivingPacifism = findPermanent(player1, "Pacifism");
        assertThat(survivingPacifism).isNotNull();
        assertThat(survivingPacifism.getAttachedTo()).isEqualTo(bears.getId());
        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Holy Strength");
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addHakim(Player player) {
        Permanent perm = new Permanent(new HakimLoreweaver());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void attachAura(Player player, Card auraCard, Permanent host) {
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(host.getId());
        harness.getGameData().playerBattlefields.get(player.getId()).add(aura);
    }

    private void beginUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UPKEEP);
    }

    private void addToGraveyard(Player player, Card card) {
        harness.getGameData().playerGraveyards.get(player.getId()).add(card);
    }

}
