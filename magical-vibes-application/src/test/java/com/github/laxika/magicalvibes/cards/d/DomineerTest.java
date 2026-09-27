package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.c.CreepingMold;
import com.github.laxika.magicalvibes.cards.g.GoblinStriker;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Domineer.class, Ornithopter.class, GoblinStriker.class, AetherSpellbomb.class,
        CreepingMold.class})
class DomineerTest extends BaseCardTest {

    @Test
    void resolvingDomineerStealsArtifactCreature() {
        Permanent artifactCreature = addCreatureReady(player2, new Ornithopter());

        harness.setHand(player1, List.of(new Domineer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifactCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifactCreature.getId()));
    }

    @Test
    void cannotEnchantNonArtifactCreature() {
        Permanent creature = addCreatureReady(player2, new GoblinStriker());

        harness.setHand(player1, List.of(new Domineer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEnchantNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2,
                new AetherSpellbomb());

        harness.setHand(player1, List.of(new Domineer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void relinquishesControlWhenDomineerLeavesTheBattlefield() {
        Permanent artifactCreature = addCreatureReady(player2, new Ornithopter());

        harness.setHand(player1, List.of(new Domineer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, artifactCreature.getId());
        harness.passBothPriorities();

        UUID domineerId = harness.getPermanentId(player1, "Domineer");
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, domineerId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifactCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifactCreature.getId()));
    }
}
