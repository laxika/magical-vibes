package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThrashThreatTest extends BaseCardTest {

    private static final int THRASH = 0;
    private static final int THREAT = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Thrash deals damage equal to the source creature's power to an opposing creature")
    void thrashDealsPowerDamageToCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalInstant(player1, 0, THRASH, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Thrash can target an opposing planeswalker")
    void thrashDealsPowerDamageToPlaneswalker() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castModalInstant(player1, 0, THRASH, List.of(source.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threat creates a 4/4 red and green Beast with trample")
    void threatCreatesBeastToken() {
        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorcery(player1, 0, THREAT, List.of());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(token.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Fuse resolves Thrash before Threat")
    void fuseResolvesBothHalvesInOrder() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, FUSE, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    @DisplayName("Thrash cannot target a creature you control")
    void thrashCannotTargetOwnCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, THRASH, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
