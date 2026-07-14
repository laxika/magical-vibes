package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UktabiWildcatsTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of Forests you control")
    void ptEqualsControlledForests() {
        Permanent wildcats = addWildcatsReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, wildcats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wildcats)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only your Forests, not opponent Forests")
    void countsOnlyControllersForests() {
        Permanent wildcats = addWildcatsReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, wildcats)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wildcats)).isEqualTo(1);
    }

    @Test
    @DisplayName("{G}, Sacrifice a Forest grants a regeneration shield")
    void regenerationSacrificesForestAndGrantsShield() {
        Permanent wildcats = addWildcatsReady(player1);
        Permanent forest1 = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, wildcatsIndex(player1), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest1.getId());
        harness.passBothPriorities();

        assertThat(wildcats.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        // One Forest left → still on the battlefield as a 1/1
        assertThat(gqs.getEffectivePower(gd, wildcats)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the regeneration ability with no Forest to sacrifice")
    void cannotActivateWithoutForest() {
        addWildcatsReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, wildcatsIndex(player1), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addWildcatsReady(Player player) {
        UktabiWildcats card = new UktabiWildcats();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int wildcatsIndex(Player player) {
        var battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals("Uktabi Wildcats")) {
                return i;
            }
        }
        throw new IllegalStateException("Uktabi Wildcats not found");
    }
}
