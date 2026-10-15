package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FootlightFiend;
import com.github.laxika.magicalvibes.cards.p.PrismaticLace;
import com.github.laxika.magicalvibes.cards.s.SkilledAnimator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlassOfTheGuildpact.class, WoollyThoctar.class, GrizzlyBears.class,
        QasaliAmbusher.class, FootlightFiend.class, SkilledAnimator.class, PrismaticLace.class})
class GlassOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts multicolored creatures you control")
    void boostsOwnMulticoloredCreatures() {
        harness.addToBattlefield(player1, new GlassOfTheGuildpact());
        Permanent woollyThoctar = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());

        assertThat(gqs.getEffectivePower(gd, woollyThoctar)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, woollyThoctar)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not boost monocolored or opponent creatures")
    void doesNotBoostMonocoloredOrOpponentCreatures() {
        harness.addToBattlefield(player1, new GlassOfTheGuildpact());
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent qasaliAmbusher = harness.addToBattlefieldAndReturn(player2, new QasaliAmbusher());

        assertThat(gqs.getEffectivePower(gd, grizzlyBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grizzlyBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, qasaliAmbusher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, qasaliAmbusher)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus disappears when Glass of the Guildpact leaves the battlefield")
    void bonusDisappearsWhenGlassLeavesBattlefield() {
        harness.addToBattlefield(player1, new GlassOfTheGuildpact());
        Permanent woollyThoctar = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());
        assertThat(gqs.getEffectivePower(gd, woollyThoctar)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Glass of the Guildpact"));

        assertThat(gqs.getEffectivePower(gd, woollyThoctar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, woollyThoctar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Hybrid creatures are multicolored and multiple Glass bonuses stack")
    void boostsHybridCreatureWithEachGlass() {
        harness.addToBattlefield(player1, new GlassOfTheGuildpact());
        harness.addToBattlefield(player1, new GlassOfTheGuildpact());
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new FootlightFiend());

        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(3);
    }

    @Test
    @CardUsed({SkilledAnimator.class, PrismaticLace.class})
    @DisplayName("Glass boosts itself when it becomes a multicolored creature")
    void boostsItselfWhenAnimatedAndMulticolored() {
        Permanent glass = harness.addToBattlefieldAndReturn(player1, new GlassOfTheGuildpact());
        harness.setHand(player1, List.of(new SkilledAnimator()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 0, glass.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, glass)).isTrue();
        assertThat(gqs.getEffectivePower(gd, glass)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, glass)).isEqualTo(5);

        harness.setHand(player1, List.of(new PrismaticLace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, glass.getId());
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectivePower(gd, glass)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, glass)).isEqualTo(6);
    }
}
