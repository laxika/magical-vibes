package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JeskaiRunemark.class, GrizzlyBears.class, GoblinPiker.class, EliteVanguard.class})
class JeskaiRunemarkTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsPlusTwoPlusTwo() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void enchantedCreatureHasFlyingWhileControllerControlsRedPermanent() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(bears);
        harness.addToBattlefield(player1, new GoblinPiker());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    void enchantedCreatureHasFlyingWhileControllerControlsWhitePermanent() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(bears);
        harness.addToBattlefield(player1, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    void enchantedCreatureDoesNotHaveFlyingWithoutRedOrWhitePermanent() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    void flyingStopsWhenRedOrWhitePermanentLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(bears);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(goblin);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    void canEnchantOpponentsCreatureAndUsesAuraControllersPermanents() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.setHand(player1, List.of(new JeskaiRunemark()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Jeskai Runemark").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    void enchantedCreaturesControllersPermanentsDoNotEnableFlying() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.addToBattlefield(player2, new EliteVanguard());
        addAura(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void enchantedRedCreatureItselfEnablesFlying() {
        Permanent goblin = addCreatureReady(player1, new GoblinPiker());
        addAura(goblin);

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FLYING)).isTrue();
    }

    @Test
    void flyingRemainsWhenAnotherQualifyingPermanentIsStillControlled() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(bears);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(goblin);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    private void addAura(Permanent enchantedCreature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new JeskaiRunemark());
        aura.setAttachedTo(enchantedCreature.getId());
    }
}
