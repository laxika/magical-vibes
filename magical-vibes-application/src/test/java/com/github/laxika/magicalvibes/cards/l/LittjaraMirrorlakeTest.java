package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LittjaraMirrorlakeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new LittjaraMirrorlake()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Littjara Mirrorlake").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one blue mana")
    void tapAddsBlueMana() {
        Permanent mirrorlake = addReady(new LittjaraMirrorlake());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mirrorlake.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates a token copy with an additional +1/+1 counter")
    void createsTokenCopyWithCounter() {
        addReady(new LittjaraMirrorlake());
        Permanent bears = addReady(new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Littjara Mirrorlake");
    }

    @Test
    @DisplayName("Can target only a creature you control")
    void cannotTargetOpponentCreature() {
        addReady(new LittjaraMirrorlake());
        Permanent opponentCreature = addReady(player2, new GrizzlyBears());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation is sorcery speed only")
    void sorcerySpeedOnly() {
        addReady(new LittjaraMirrorlake());
        Permanent bears = addReady(new GrizzlyBears());
        addManaForAbility();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Card card) {
        return addReady(player1, card);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
