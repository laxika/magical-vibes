package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LacerateFlesh.class, AirElemental.class, GrizzlyBears.class})
class LacerateFleshTest extends BaseCardTest {

    @Test
    void createsBloodTokensForExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
    }

    @Test
    void createsNoBloodTokensWhenDamageIsNotExcess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(target);

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new LacerateFlesh()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsDamageAlreadyMarkedWhenDeterminingExcess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setMarkedDamage(2);

        cast(target);

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
        assertThat(countPermanents(player2, "Blood")).isZero();
    }

    @Test
    void canTargetOwnCreatureAndCreatesTokensForCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(target);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
        assertThat(countPermanents(player2, "Blood")).isZero();
    }

    @Test
    void createsNoBloodWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LacerateFlesh()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lacerate Flesh");
        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    @Test
    void createdBloodCanDiscardAndSacrificeToDrawImmediately() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);
        Permanent blood = findPermanent(player1, "Blood");
        LacerateFlesh discarded = new LacerateFlesh();
        LacerateFlesh drawn = new LacerateFlesh();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blood);
        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new LacerateFlesh()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
