package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FootChopper.class, GrizzlyBears.class, Forest.class})
class FootChopperTest extends BaseCardTest {

    @Test
    @DisplayName("Living weapon creates a 1/1 Ninja and gives it flying")
    void livingWeaponCreatesNinjaWithFlying() {
        harness.setHand(player1, List.of(new FootChopper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent equipment = findPermanent(player1, "Foot Chopper");
        Permanent ninja = findPermanent(player1, "Ninja");
        assertThat(equipment.getAttachedTo()).isEqualTo(ninja.getId());
        assertThat(ninja.getCard().getPower()).isEqualTo(1);
        assertThat(ninja.getCard().getToughness()).isEqualTo(1);
        assertThat(ninja.getCard().getSubtypes()).contains(CardSubtype.NINJA);
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Accepting the combat-damage trigger sacrifices the equipped creature and draws its power")
    void acceptingTriggerSacrificesCreatureAndDrawsPower() {
        Permanent creature = attachFootChopperToBears(player1);
        creature.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the combat-damage trigger leaves the equipped creature attached")
    void decliningTriggerLeavesCreatureAttached() {
        Permanent creature = attachFootChopperToBears(player1);
        Permanent equipment = findPermanent(player1, "Foot Chopper");
        creature.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent attachFootChopperToBears(Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        Permanent equipment = new Permanent(new FootChopper());
        equipment.setSummoningSick(false);
        equipment.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player.getId()).add(equipment);
        return creature;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
