package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HeatedArgumentTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has 6 damage to creature + optional exile-then-2-damage effects")
    void cardStructure() {
        HeatedArgument card = new HeatedArgument();
        var effects = card.getEffects(EffectSlot.SPELL);

        assertThat(effects).hasSize(2);
        assertThat(effects.get(0)).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        assertThat(((DealDamageToTargetCreatureEffect) effects.get(0)).damage()).isEqualTo(6);
        assertThat(effects.get(1)).isInstanceOf(MayEffect.class);
        assertThat(((MayEffect) effects.get(1)).wrapped())
                .isInstanceOf(ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect.class);
        assertThat(((ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect)
                ((MayEffect) effects.get(1)).wrapped()).damage()).isEqualTo(2);
    }

    // ===== Damage only, exile declined =====

    @Test
    @DisplayName("Deals 6 to creature; declining the exile deals no damage to controller")
    void declineExileNoControllerDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new HeatedArgument()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4); // 4 generic + 1 red

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline exile

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears"); // 6 kills 2/2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20); // no controller damage
        // Graveyard card not exiled
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2); // original card + Heated Argument
    }

    // ===== Exile accepted =====

    @Test
    @DisplayName("Accepting the exile deals 2 to the creature's controller and exiles a graveyard card")
    void acceptExileDamagesControllerAndExiles() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new HeatedArgument()));
        GrizzlyBears gyCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(gyCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // exile a card

        GameData gd = harness.getGameData();
        // Avatar (8/8) survives 6 damage
        harness.assertOnBattlefield(player2, "Avatar of Might");
        assertThat(avatar.getMarkedDamage()).isEqualTo(6);
        // Controller takes 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Graveyard card was exiled
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(gyCard);
        assertThat(gd.exiledCards).anyMatch(e -> e.card() == gyCard);
    }

    // ===== Accept but empty graveyard =====

    @Test
    @DisplayName("Accepting with an empty graveyard deals no controller damage")
    void acceptWithEmptyGraveyardNoDamage() {
        harness.addToBattlefield(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new HeatedArgument()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // accept, but nothing to exile

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20); // no controller damage
    }
}
