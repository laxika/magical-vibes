package com.github.laxika.magicalvibes.cards.i;

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

class InfestedFleshcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches the Equipment and gives the creature +2/+0")
    void equipBoostsCreature() {
        harness.addToBattlefield(player1, new InfestedFleshcutter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, creatureId);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with the equipped creature creates a toxic Mite that can't block")
    void attackTriggerCreatesMite() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cutter = addCutter(player1);
        cutter.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent mite = findPermanent(player1, "Mite");
        assertThat(mite.getCard().isToken()).isTrue();
        assertThat(mite.getCard().getSubtypes()).containsExactly(CardSubtype.PHYREXIAN, CardSubtype.MITE);
        assertThat(gqs.hasKeyword(gd, mite, Keyword.TOXIC)).isTrue();
        assertThat(bls.canBlock(gd, mite)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger does not fire while the Equipment is unattached")
    void noTriggerWhenUnattached() {
        addCreatureReady(player1, new GrizzlyBears());
        addCutter(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Infested Fleshcutter"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Mite"));
    }

    private Permanent addCutter(Player player) {
        Permanent permanent = new Permanent(new InfestedFleshcutter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
