package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeeperOfTheCadenceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts target artifact, instant, or sorcery cards on the bottom of their owners' libraries")
    void tucksSupportedCardTypesIntoOwnersLibraries() {
        int keeperIndex = addKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        Card artifact = new ChromaticStar();
        Card sorcery = new Divination();
        Card instant = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(artifact, sorcery)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(instant)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Island())));

        harness.activateAbilityWithGraveyardTargets(player1, keeperIndex, 0, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithGraveyardTargets(player1, keeperIndex, 0, List.of(instant.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithGraveyardTargets(player1, keeperIndex, 0, List.of(sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(sorcery.getId());
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(instant.getId());
    }

    @Test
    @DisplayName("Cannot target a creature card")
    void rejectsUnsupportedCardType() {
        int keeperIndex = addKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, keeperIndex, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private int addKeeper() {
        harness.addToBattlefield(player1, new KeeperOfTheCadence());
        Permanent keeper = findPermanent(player1, "Keeper of the Cadence");
        return gd.playerBattlefields.get(player1.getId()).indexOf(keeper);
    }
}
