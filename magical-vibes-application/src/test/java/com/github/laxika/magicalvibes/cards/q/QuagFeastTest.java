package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuagFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Mills two cards before destroying a creature within the graveyard threshold")
    void millsBeforeCheckingThreshold() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        cast(List.of(), targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Does not destroy a target whose mana value is above the caster's graveyard size")
    void doesNothingAboveGraveyardThreshold() {
        Permanent planeswalker = addReadyPlaneswalker(player2);

        cast(List.of(), planeswalker.getId());

        harness.assertOnBattlefield(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Can destroy a Vehicle")
    void destroysVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());

        cast(List.of(new Forest(), new Forest(), new Forest(), new Forest()), vehicle.getId());

        harness.assertNotOnBattlefield(player2, "Air Response Unit");
    }

    @Test
    @DisplayName("Cannot target a permanent that is not a creature, planeswalker, or Vehicle")
    void rejectsOtherPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new QuagFeast()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<Card> graveyard, UUID targetId) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new QuagFeast()));
        addCastingMana();

        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyPlaneswalker(Player player) {
        Permanent permanent = new Permanent(new GarrukWildspeaker());
        permanent.setCounterCount(CounterType.LOYALTY, 3);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
