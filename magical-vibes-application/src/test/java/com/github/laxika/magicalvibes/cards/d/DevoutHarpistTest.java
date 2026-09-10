package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TreacherousLink;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevoutHarpist.class, TreacherousLink.class, WildGrowth.class, Forest.class})
class DevoutHarpistTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an Aura attached to a creature")
    void destroysAuraAttachedToCreature() {
        addReadyHarpist(player1);
        Permanent creature = addCreatureReady(player2, new DevoutHarpist());
        Permanent aura = addAuraAttachedTo(player2, new TreacherousLink(), creature);

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Treacherous Link");
        harness.assertInGraveyard(player2, "Treacherous Link");
    }

    @Test
    @DisplayName("Cannot target an Aura that is not attached to a creature")
    void cannotTargetUnattachedAura() {
        addReadyHarpist(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new TreacherousLink());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an Aura attached to a noncreature permanent")
    void cannotTargetAuraAttachedToNonCreature() {
        addReadyHarpist(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = addAuraAttachedTo(player2, new WildGrowth(), land);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyHarpist(player1);
        Permanent creature = addCreatureReady(player2, new DevoutHarpist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHarpist(Player player) {
        return addCreatureReady(player, new DevoutHarpist());
    }

    private Permanent addAuraAttachedTo(Player player, Card auraCard, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, auraCard);
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
