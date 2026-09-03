package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavenousBaloth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ContestedCliffs.class, RavenousBaloth.class, GrizzlyBears.class})
class ContestedCliffsTest extends BaseCardTest {

    @Test
    void tappingAddsColorlessMana() {
        Permanent cliffs = addReadyCliffs();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cliffs.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void beastFightsOpponentsCreature() {
        Permanent cliffs = addReadyCliffs();
        Permanent baloth = addReadyPermanent(player1, new RavenousBaloth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID balothId = baloth.getId();
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(balothId, bearsId));
        harness.passBothPriorities();

        assertThat(cliffs.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ravenous Baloth");
        assertThat(baloth.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetNonBeastAsFirstTarget() {
        addReadyCliffs();
        Permanent bears = addReadyPermanent(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(bears.getId(), opponentBearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Beast creature you control");
    }

    @Test
    void cannotTargetOwnCreatureAsSecondTarget() {
        addReadyCliffs();
        Permanent baloth = addReadyPermanent(player1, new RavenousBaloth());
        Permanent bears = addReadyPermanent(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(baloth.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    private Permanent addReadyCliffs() {
        return addReadyPermanent(player1, new ContestedCliffs());
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
