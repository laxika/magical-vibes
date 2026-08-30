package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.e.Evaporate;
import com.github.laxika.magicalvibes.cards.i.IronclawCurse;
import com.github.laxika.magicalvibes.cards.r.Retribution;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaSprite.class, Retribution.class, SpectralBears.class, Shrink.class,
        Evaporate.class, DragonWhelp.class, IronclawCurse.class})
class SeaSpriteTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be targeted by a red spell")
    void cannotBeTargetedByRedSpell() {
        Permanent sprite = addCreatureReady(player2, new SeaSprite());
        Permanent otherCreature = addCreatureReady(player2, new SpectralBears());

        harness.setHand(player1, List.of(new Retribution()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, sprite.getId(),
                List.of(otherCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by a green instant")
    void canBeTargetedByGreenInstant() {
        Permanent sprite = addCreatureReady(player1, new SeaSprite());

        harness.setHand(player1, List.of(new Shrink()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, sprite.getId());

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(-4);
    }

    @Test
    @DisplayName("Prevents damage from red sources")
    void preventsDamageFromRedSources() {
        Permanent sprite = addCreatureReady(player1, new SeaSprite());

        harness.setHand(player1, List.of(new Evaporate()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sprite);
        assertThat(sprite.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot be blocked by a red creature")
    void cannotBeBlockedByRedCreature() {
        addCreatureReady(player1, new SeaSprite());
        addCreatureReady(player2, new DragonWhelp());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be enchanted by a red Aura")
    void cannotBeEnchantedByRedAura() {
        Permanent sprite = addCreatureReady(player1, new SeaSprite());

        harness.setHand(player1, List.of(new IronclawCurse()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, sprite.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }
}
