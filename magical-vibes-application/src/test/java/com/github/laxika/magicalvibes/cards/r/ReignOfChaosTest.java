package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JungleWurm;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TeferisDrake;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReignOfChaos.class, Plains.class, Island.class, ZhalfirinKnight.class, TeferisDrake.class, JungleWurm.class})
class ReignOfChaosTest extends BaseCardTest {

    private Card whitePlainsCreature() {
        Card card = new Card();
        card.setName("White Plains Creature");
        card.setType(CardType.LAND);
        card.setAdditionalTypes(java.util.Set.of(CardType.CREATURE));
        card.setColors(List.of(CardColor.WHITE));
        card.setSubtypes(List.of(CardSubtype.PLAINS));
        return card;
    }

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Mode 0 destroys target Plains and target white creature")
    void mode0DestroysPlainsAndWhiteCreature() {
        harness.setHand(player1, List.of(new ReignOfChaos()));
        giveMana();
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new ZhalfirinKnight());
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new JungleWurm());

        harness.castModalSorcery(player1, 0, 0, List.of(plains.getId(), knight.getId()));
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).extracting(Permanent::getId)
                .doesNotContain(plains.getId(), knight.getId())
                .contains(wurm.getId());
    }

    @Test
    @DisplayName("Mode 0 may target one permanent for both target requirements")
    void mode0MayTargetOnePlainsWhiteCreatureTwice() {
        harness.setHand(player1, List.of(new ReignOfChaos()));
        giveMana();
        Permanent target = harness.addToBattlefieldAndReturn(player2, whitePlainsCreature());

        harness.castModalSorcery(player1, 0, 0, List.of(target.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(target.getId());
    }

    @Test
    @DisplayName("Mode 0 cannot target an Island as its land target")
    void mode0RejectsIsland() {
        harness.setHand(player1, List.of(new ReignOfChaos()));
        giveMana();
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new ZhalfirinKnight());

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(island.getId(), knight.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 1 destroys target Island and target blue creature")
    void mode1DestroysIslandAndBlueCreature() {
        harness.setHand(player1, List.of(new ReignOfChaos()));
        giveMana();
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new TeferisDrake());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.castModalSorcery(player1, 0, 1, List.of(island.getId(), drake.getId()));
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).extracting(Permanent::getId)
                .doesNotContain(island.getId(), drake.getId())
                .contains(plains.getId());
    }

    @Test
    @DisplayName("Mode 1 cannot target a non-blue creature")
    void mode1RejectsNonBlueCreature() {
        harness.setHand(player1, List.of(new ReignOfChaos()));
        giveMana();
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new JungleWurm());

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(island.getId(), wurm.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 0 cannot target a non-white creature")
    void mode0RejectsNonWhiteCreature() {
        harness.setHand(player1, List.of(new ReignOfChaos()));
        giveMana();
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new JungleWurm());

        UUID plainsId = plains.getId();
        UUID wurmId = wurm.getId();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(plainsId, wurmId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 1 cannot target a Plains as its land target")
    void mode1RejectsPlains() {
        harness.setHand(player1, List.of(new ReignOfChaos()));
        giveMana();
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new TeferisDrake());

        UUID plainsId = plains.getId();
        UUID drakeId = drake.getId();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(plainsId, drakeId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
