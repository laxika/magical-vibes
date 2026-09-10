package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenthicBehemoth;
import com.github.laxika.magicalvibes.cards.c.Capsize;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.e.ElvishFury;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.r.RootwaterHunter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scragnoth.class, Counterspell.class, Capsize.class, ElvishFury.class,
        ShimmeringWings.class, BenthicBehemoth.class, LowlandGiant.class, RootwaterHunter.class})
class ScragnothTest extends BaseCardTest {

    @Test
    @DisplayName("Scragnoth cannot be countered by Counterspell")
    void cannotBeCounteredByCounterspell() {
        Scragnoth scragnoth = new Scragnoth();
        harness.setHand(player1, List.of(scragnoth));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, scragnoth.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scragnoth");
        harness.assertNotInGraveyard(player1, "Scragnoth");
        harness.assertInGraveyard(player2, "Counterspell");
    }

    @Test
    @DisplayName("Cannot be targeted by a blue instant")
    void cannotBeTargetedByBlueInstant() {
        Permanent scragnoth = addScragnoth(player2);
        addCreatureReady(player2, new BenthicBehemoth());

        harness.setHand(player1, List.of(new Capsize()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, scragnoth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Blue abilities cannot target Scragnoth")
    void cannotBeTargetedByBlueAbility() {
        Permanent scragnoth = addScragnoth(player2);
        addCreatureReady(player1, new RootwaterHunter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, scragnoth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("A blue Aura cannot enchant Scragnoth")
    void cannotBeEnchantedByBlueAura() {
        Permanent scragnoth = addScragnoth(player2);
        addCreatureReady(player2, new BenthicBehemoth());

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, scragnoth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Can be targeted by a green instant")
    void canBeTargetedByGreenInstant() {
        Permanent scragnoth = addScragnoth(player1);
        ElvishFury elvishFury = new ElvishFury();

        harness.setHand(player1, List.of(elvishFury));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, scragnoth.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(elvishFury);
    }

    @Test
    @DisplayName("Blue creature cannot block Scragnoth")
    void blueCreatureCannotBlock() {
        Permanent attacker = addScragnoth(player1);
        attacker.setAttacking(true);

        addCreatureReady(player2, new BenthicBehemoth());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Scragnoth takes no combat damage from a blue creature")
    void takesNoDamageFromBlueCreature() {
        Permanent attacker = addCreatureReady(player1, new BenthicBehemoth());
        attacker.setAttacking(true);

        Permanent blocker = addScragnoth(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player2, "Scragnoth");
        harness.assertOnBattlefield(player1, "Benthic Behemoth");
    }

    @Test
    @DisplayName("Scragnoth dies to lethal damage from a red creature")
    void takesDamageFromRedCreature() {
        Permanent attacker = addCreatureReady(player1, new LowlandGiant());
        attacker.setAttacking(true);

        Permanent blocker = addScragnoth(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player2, "Scragnoth");
    }

    private Permanent addScragnoth(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new Scragnoth());
    }
}
