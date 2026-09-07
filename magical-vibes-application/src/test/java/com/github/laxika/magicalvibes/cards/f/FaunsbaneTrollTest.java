package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaunsbaneTroll.class, GrizzlyBears.class})
class FaunsbaneTrollTest extends BaseCardTest {

    @Test
    void entersWithMonsterRoleAttachedAndGetsItsBonus() {
        Permanent troll = castTroll();

        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().isAura()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(troll.getId());
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void sacrificesAttachedRoleFightsAndExilesCreatureThatWouldDie() {
        Permanent troll = castTroll();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(troll),
                0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Monster");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(troll);
    }

    @Test
    void cannotActivateWithoutAnAuraAttachedToThisCreature() {
        Permanent troll = addReadyTroll();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(troll),
                0,
                null,
                target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castTroll() {
        harness.setHand(player1, List.of(new FaunsbaneTroll()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Faunsbane Troll");
    }

    private Permanent addReadyTroll() {
        Permanent troll = new Permanent(new FaunsbaneTroll());
        troll.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(troll);
        return troll;
    }
}
