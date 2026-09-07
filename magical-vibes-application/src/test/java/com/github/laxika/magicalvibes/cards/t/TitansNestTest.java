package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TitansNest.class, Fireball.class, GrizzlyBears.class, MindStone.class})
class TitansNestTest extends BaseCardTest {

    @Test
    void upkeepSurveilsOne() {
        harness.addToBattlefield(player1, new TitansNest());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void exilingAGraveyardCardAddsRestrictedColorlessMana() {
        harness.addToBattlefield(player1, new TitansNest());
        Card exiledCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(exiledCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getColoredSpellWithoutXOnlyColorless()).isEqualTo(1);
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(exiledCard);
    }

    @Test
    void restrictedManaPaysForColoredSpellWithoutX() {
        harness.addToBattlefield(player1, new TitansNest());
        Card exiledCard = new MindStone();
        harness.setGraveyard(player1, List.of(exiledCard));
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getColoredSpellWithoutXOnlyColorless()).isZero();
    }

    @Test
    void restrictedManaCannotPayForColorlessOrXSpells() {
        harness.addToBattlefield(player1, new TitansNest());
        Card firstExiledCard = new GrizzlyBears();
        Card secondExiledCard = new MindStone();
        harness.setGraveyard(player1, List.of(firstExiledCard, secondExiledCard));
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MindStone()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Fireball()));
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getColoredSpellWithoutXOnlyColorless()).isEqualTo(2);
    }
}
