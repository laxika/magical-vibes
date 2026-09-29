package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReflectionNet.class, GrizzlyBears.class, HillGiant.class})
class ReflectionNetTest extends BaseCardTest {

    @Test
    void exilesTargetCreatureUntilReflectionNetLeaves() {
        Permanent exiledCreature = addReadyCreature(player2, new HillGiant());
        Permanent net = castReflectionNet(exiledCreature);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(exiledCreature);
        assertThat(gd.findExiledCard(exiledCreature.getOriginalCard().getId()).sourcePermanentId())
                .isEqualTo(net.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, net));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == exiledCreature.getOriginalCard());
    }

    @Test
    void targetCreatureBecomesCopyOfExiledCreatureAndAbilityIsOneShot() {
        Permanent exiledCreature = addReadyCreature(player2, new HillGiant());
        castReflectionNet(exiledCreature);
        Permanent target = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(target.getCard().getPower()).isEqualTo(3);
        assertThat(target.getCard().getToughness()).isEqualTo(3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can be activated only once");
    }

    @Test
    void abilityCannotTargetAnOpponentCreature() {
        Permanent exiledCreature = addReadyCreature(player2, new HillGiant());
        castReflectionNet(exiledCreature);
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    private Permanent castReflectionNet(Permanent target) {
        Card netCard = new ReflectionNet();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(netCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == netCard)
                .findFirst()
                .orElseThrow();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
