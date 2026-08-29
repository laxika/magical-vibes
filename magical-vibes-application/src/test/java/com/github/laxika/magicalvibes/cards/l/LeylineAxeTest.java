package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeylineAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline Axe in opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        openingHarness.setHand(openingPlayer, List.of(new LeylineAxe()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingPlayer, true);

        assertThat(openingHarness.getGameData().playerBattlefields.get(openingPlayer.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline Axe"));
    }

    @Test
    @DisplayName("Casting Leyline Axe and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new LeylineAxe()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline Axe") && !p.isAttached());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1, double strike, and trample")
    void equippedCreatureGetsBoostAndKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Resolving equip attaches Leyline Axe to a creature you control")
    void resolvingEquipAttachesToCreature() {
        Permanent axe = addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addAxeReady(Player player) {
        Permanent perm = new Permanent(new LeylineAxe());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
