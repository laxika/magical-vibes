package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdamantWill;
import com.github.laxika.magicalvibes.cards.c.CircleOfConfinement;
import com.github.laxika.magicalvibes.cards.d.DrogskolInfantry;
import com.github.laxika.magicalvibes.cards.f.ForebodingStatue;
import com.github.laxika.magicalvibes.cards.w.WeddingInvitation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sanctify.class, CircleOfConfinement.class, DrogskolInfantry.class,
        WeddingInvitation.class, AdamantWill.class, ForebodingStatue.class})
class SanctifyTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new Sanctify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Destroys target artifact and gains 3 life")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new WeddingInvitation());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Wedding Invitation");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Wedding Invitation");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Destroys target enchantment and gains 3 life")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new CircleOfConfinement());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Circle of Confinement");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Circle of Confinement");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new DrogskolInfantry());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Drogskol Infantry");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("Can destroy its controller's artifact")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new WeddingInvitation());
        prepare();

        harness.castAndResolveSorcery(player1, 0,
                harness.getPermanentId(player1, "Wedding Invitation"));

        harness.assertInGraveyard(player1, "Wedding Invitation");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not gain life when its only target is sacrificed in response")
    void doesNotGainLifeWhenTargetLeaves() {
        harness.addToBattlefield(player2, new WeddingInvitation());
        harness.addToBattlefield(player2, new DrogskolInfantry());
        prepare();

        harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Wedding Invitation"));
        harness.activateAbility(player2, 0, null,
                harness.getPermanentId(player2, "Drogskol Infantry"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wedding Invitation");
        harness.assertInGraveyard(player1, "Sanctify");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Gains life even when an indestructible artifact creature survives")
    void gainsLifeWhenTargetIsIndestructible() {
        harness.addToBattlefield(player2, new ForebodingStatue());
        harness.setHand(player2, List.of(new AdamantWill()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        prepare();
        UUID targetId = harness.getPermanentId(player2, "Foreboding Statue");

        harness.castSorcery(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Foreboding Statue");
        harness.assertInGraveyard(player1, "Sanctify");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }
}
