package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GarruksPackleader;
import com.github.laxika.magicalvibes.cards.i.IcehideTroll;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LittjaraMirrorlake.class, IcehideTroll.class, GarruksPackleader.class})
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
        Permanent troll = addReady(new IcehideTroll());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, troll.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Icehide Troll");
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Littjara Mirrorlake");
    }

    @Test
    @DisplayName("Can target only a creature you control")
    void cannotTargetOpponentCreature() {
        addReady(new LittjaraMirrorlake());
        Permanent opponentCreature = addReady(player2, new IcehideTroll());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation is sorcery speed only")
    void sorcerySpeedOnly() {
        addReady(new LittjaraMirrorlake());
        Permanent troll = addReady(new IcehideTroll());
        addManaForAbility();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, troll.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The land is sacrificed and mana is paid before the ability resolves")
    void paysCostsImmediately() {
        addReady(new LittjaraMirrorlake());
        Permanent troll = addReady(new IcehideTroll());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, troll.getId());

        harness.assertInGraveyard(player1, "Littjara Mirrorlake");
        harness.assertNotOnBattlefield(player1, "Littjara Mirrorlake");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Counters already on the original creature are not copied")
    void doesNotCopyOriginalCounters() {
        addReady(new LittjaraMirrorlake());
        Permanent troll = addReady(new IcehideTroll());
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, troll.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("No token is created if the target leaves before resolution")
    void targetLeavesBeforeResolution() {
        addReady(new LittjaraMirrorlake());
        Permanent troll = addReady(new IcehideTroll());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, troll.getId());
        gd.playerBattlefields.get(player1.getId()).remove(troll);
        gd.playerGraveyards.get(player1.getId()).add(troll.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Littjara Mirrorlake");
    }

    @Test
    @DisplayName("Cannot activate during combat on your own turn")
    void cannotActivateDuringCombat() {
        addReady(new LittjaraMirrorlake());
        Permanent troll = addReady(new IcehideTroll());
        addManaForAbility();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, troll.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The entering counter counts toward power-based enter triggers")
    void counterIsPresentForEnterTriggers() {
        addReady(new LittjaraMirrorlake());
        Permanent troll = addReady(new IcehideTroll());
        harness.addToBattlefield(player1, new GarruksPackleader());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, troll.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReady(Card card) {
        return addReady(player1, card);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
