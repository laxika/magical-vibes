package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LumenClassFrigate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeedshipBroodtender.class, Forest.class, GrizzlyBears.class, LumenClassFrigate.class})
class SeedshipBroodtenderTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it mills three cards")
    void entersMillsThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castSeedshipBroodtender();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Its activated ability sacrifices it and returns a creature")
    void returnsCreatureFromGraveyard() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(broodtender), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Seedship Broodtender");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Its activated ability also returns a Spacecraft")
    void returnsSpacecraftFromGraveyard() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card spacecraft = new LumenClassFrigate();
        harness.setGraveyard(player1, List.of(spacecraft));
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(broodtender), 0, null, spacecraft.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Seedship Broodtender");
        harness.assertOnBattlefield(player1, "Lumen-Class Frigate");
    }

    @Test
    @DisplayName("Its activated ability rejects a noncreature, non-Spacecraft card")
    void rejectsInvalidGraveyardTarget() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(broodtender), 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Seedship Broodtender");
        harness.assertInGraveyard(player1, "Forest");
    }

    private void castSeedshipBroodtender() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SeedshipBroodtender()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
