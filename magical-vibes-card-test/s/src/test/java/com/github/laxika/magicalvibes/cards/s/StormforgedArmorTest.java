package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormforgedArmor.class, GrizzlyBears.class})
class StormforgedArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature attacks and conjures Ball Lightning tapped and attacking")
    void attackConjuresBallLightningTappedAndAttacking() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent ballLightning = findPermanent(player1, "Ball Lightning");
        assertThat(ballLightning.getCard().isToken()).isFalse();
        assertThat(ballLightning.isTapped()).isTrue();
        assertThat(ballLightning.isAttacking()).isTrue();
        assertThat(ballLightning.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Equip—Pay 3 life attaches Stormforged Armor")
    void equipPaysLifeAndAttaches() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An unattached Stormforged Armor does not trigger")
    void unattachedArmorDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        addArmorReady(player1);

        declareAttackers(player1, List.of(0));

        assertThat(findPermanents(player1, "Ball Lightning")).isEmpty();
    }

    private Permanent addArmorReady(Player player) {
        Permanent permanent = new Permanent(new StormforgedArmor());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
