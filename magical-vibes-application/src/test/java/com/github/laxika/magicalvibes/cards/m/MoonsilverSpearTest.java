package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MoonsilverSpearTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {4} attaches the Equipment and grants first strike")
    void equipGrantsFirstStrike() {
        harness.addToBattlefield(player1, new MoonsilverSpear());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        Permanent spear = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(spear.getAttachedTo()).isEqualTo(bearsId);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Whenever equipped creature attacks, create a 4/4 white Angel with flying")
    void attackTriggerCreatesAngelToken() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent spear = addSpear(player1);
        spear.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angel"))
                .findFirst()
                .orElseThrow();
        assertThat(angel.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
    }

    @Test
    @DisplayName("No trigger when the Equipment is not attached")
    void noTriggerWhenUnattached() {
        addCreatureReady(player1, new GrizzlyBears());
        addSpear(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Moonsilver Spear"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angel"));
    }

    private Permanent addSpear(Player player) {
        Permanent perm = new Permanent(new MoonsilverSpear());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
