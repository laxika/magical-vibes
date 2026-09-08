package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainsClaws.class, GrizzlyBears.class})
class CaptainsClawsTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {1} attaches Captain's Claws and gives the creature +1/+0")
    void equipBoostsCreature() {
        harness.addToBattlefield(player1, new CaptainsClaws());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with the equipped creature creates a tapped and attacking Kor Ally")
    void attackCreatesTappedAndAttackingKorAlly() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent claws = addClawsReady(player1);
        claws.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent token = findPermanent(player1, "Kor Ally");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KOR, CardSubtype.ALLY);
    }

    @Test
    @DisplayName("An unattached Captain's Claws does not trigger when a creature attacks")
    void unattachedClawsDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        addClawsReady(player1);

        declareAttackers(player1, List.of(0));

        assertThat(findPermanents(player1, "Kor Ally")).isEmpty();
    }

    private Permanent addClawsReady(Player player) {
        Permanent permanent = new Permanent(new CaptainsClaws());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
