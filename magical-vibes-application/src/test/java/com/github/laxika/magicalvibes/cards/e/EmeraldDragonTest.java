package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.d.DissonantWave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmeraldDragon.class, DissonantWave.class, FumeSpitter.class,
        GrizzlyBears.class, Millstone.class})
class EmeraldDragonTest extends BaseCardTest {

    @Test
    void adventureCountersAnAbilityFromANoncreatureSource() {
        Millstone millstone = new Millstone();
        harness.addToBattlefield(player2, millstone);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        EmeraldDragon card = new EmeraldDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, millstone.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetAnAbilityFromACreatureSource() {
        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player2, fumeSpitter);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null,
                harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        EmeraldDragon card = new EmeraldDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, fumeSpitter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Millstone millstone = new Millstone();
        harness.addToBattlefield(player2, millstone);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        EmeraldDragon card = new EmeraldDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, millstone.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Emerald Dragon");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
