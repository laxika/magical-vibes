package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.Regress;
import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
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

@CardUsed({ContaminatedGround.class, Forest.class, Mountain.class, ArmoredTransport.class,
        Naturalize.class, Regress.class, SimicGuildgate.class})
class ContaminatedGroundTest extends BaseCardTest {

    @Test
    @DisplayName("Contaminated Ground can enchant a land")
    void canEnchantLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ContaminatedGround()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Contaminated Ground")
                        && forest.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Contaminated Ground cannot enchant a nonland permanent")
    void cannotEnchantNonland() {
        harness.addToBattlefield(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new ContaminatedGround()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Enchanted land becomes a Swamp and its controller loses 2 life when it is tapped")
    void enchantedLandBecomesSwampAndCausesLifeLoss() {
        addLandWithAura(player1);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Contaminated Ground affects the enchanted land's controller")
    void affectsEnchantedLandController() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ContaminatedGround());
        aura.setAttachedTo(land.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping an unenchanted land does not cause life loss")
    void unenchantedLandDoesNotCauseLifeLoss() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Life loss uses the land's last known controller after the land is returned to hand")
    void lifeLossStillOccursAfterLandLeavesBattlefield() {
        addLandWithAura(player2);
        Permanent land = findPermanent(player2, "Forest");
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.tapPermanent(player2, 0);
        harness.passPriority(player2);
        harness.setHand(player1, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Forest");
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Destroying the Aura does not stop a pending life-loss trigger and restores green mana")
    void auraRemovalDoesNotStopPendingTrigger() {
        addLandWithAura(player1);
        Permanent land = findPermanent(player1, "Forest");
        Permanent aura = findPermanent(player1, "Contaminated Ground");
        harness.setLife(player1, 20);
        harness.tapPermanent(player1, 0);
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Contaminated Ground");
        harness.assertLife(player1, 18);
        land.untap();
        int greenBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);
        harness.tapPermanent(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(greenBefore + 1);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A converted Guildgate produces black mana without its printed mana choice")
    void nonbasicLandLosesPrintedManaAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ContaminatedGround());
        aura.setAttachedTo(land.getId());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Each new tap of the enchanted land causes another 2 life loss")
    void repeatedTapsTriggerSeparately() {
        addLandWithAura(player1);
        Permanent land = findPermanent(player1, "Forest");
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();
        land.untap();
        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
    }

    private void addLandWithAura(Player owner) {
        Permanent land = harness.addToBattlefieldAndReturn(owner, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(owner, new ContaminatedGround());
        aura.setAttachedTo(land.getId());
    }
}
