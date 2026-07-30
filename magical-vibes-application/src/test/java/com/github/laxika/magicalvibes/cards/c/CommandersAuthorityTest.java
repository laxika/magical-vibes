package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommandersAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Commander's Authority attaches it to the target creature")
    void resolvingAttachesToCreature() {
        Permanent bears = addBears(player1);

        harness.setHand(player1, List.of(new CommandersAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Commander's Authority")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature's controller creates a Human token at their upkeep")
    void createsHumanTokenAtEnchantedControllerUpkeep() {
        Permanent bears = addBears(player1);
        attachAuthority(bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).isEqualTo(1);
        assertThat(humanTokens(player2)).isZero();
    }

    @Test
    @DisplayName("Token goes to the enchanted creature's controller, not the Aura's controller")
    void tokenGoesToEnchantedControllerNotAuraController() {
        Permanent bears = addBears(player2);
        attachAuthority(bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).isZero();
        assertThat(humanTokens(player2)).isZero();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(humanTokens(player2)).isEqualTo(1);
        assertThat(humanTokens(player1)).isZero();
    }

    @Test
    @DisplayName("A token is created each upkeep")
    void tokensAccumulateOverUpkeeps() {
        Permanent bears = addBears(player1);
        attachAuthority(bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).isEqualTo(2);
    }

    private void attachAuthority(Permanent creature) {
        Permanent aura = new Permanent(new CommandersAuthority());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private Permanent addBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private long humanTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Human"))
                .count();
    }
}
