package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ApocalypseChime;
import com.github.laxika.magicalvibes.cards.s.SeaSprite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeastOfTheUnicorn.class, SeaSprite.class, ApocalypseChime.class})
class FeastOfTheUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +4/+0")
    void enchantedCreatureGetsBuff() {
        Permanent creature = addCreatureReady(player1, new SeaSprite());

        Permanent aura = new Permanent(new FeastOfTheUnicorn());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting Feast of the Unicorn attaches it to an opponent's creature")
    void castingAttachesToOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new SeaSprite());

        harness.setHand(player1, List.of(new FeastOfTheUnicorn()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Feast of the Unicorn");
        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature returns to base stats when Feast of the Unicorn is removed")
    void effectsStopWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new SeaSprite());

        Permanent aura = new Permanent(new FeastOfTheUnicorn());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Feast of the Unicorn")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ApocalypseChime());
        harness.setHand(player1, List.of(new FeastOfTheUnicorn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
