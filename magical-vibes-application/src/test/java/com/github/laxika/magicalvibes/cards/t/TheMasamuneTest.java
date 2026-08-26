package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ExultantCultist;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMasamune.class, ExultantCultist.class, Forest.class, GrizzlyBears.class, Shock.class})
class TheMasamuneTest extends BaseCardTest {

    @Test
    @DisplayName("The Masamune grants first strike and requires a block only while attacking")
    void attackOnlyAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent masamune = addCreatureReady(player1, new TheMasamune());
        masamune.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        creature.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("The Masamune doubles a death trigger of the equipped creature")
    void doublesEquippedCreatureDeathTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent creature = addCreatureReady(player1, new ExultantCultist());
        Permanent masamune = addCreatureReady(player1, new TheMasamune());
        masamune.setAttachedTo(creature.getId());

        killWithShock(creature);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The Masamune doubles an owned emblem's creature-death trigger")
    void doublesOwnedEmblemCreatureDeathTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent masamune = addCreatureReady(player1, new TheMasamune());
        masamune.setAttachedTo(equippedCreature.getId());
        gd.emblems.add(new Emblem(player1.getId(), List.of(
                new EmblemCreatureDeathTriggerEffect(List.of(new DrawCardEffect()), null)),
                new ExultantCultist()));
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());

        killWithShock(dyingCreature);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void killWithShock(Permanent target) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
