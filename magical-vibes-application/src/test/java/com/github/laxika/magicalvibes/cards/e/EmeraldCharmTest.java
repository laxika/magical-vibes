package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmeraldCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Untap target permanent")
    class UntapMode {

        @Test
        @DisplayName("Untaps the targeted permanent")
        void untapsPermanent() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            bears.tap();
            harness.setHand(player1, List.of(new EmeraldCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castInstant(player1, 0, 0, bears.getId());
            harness.passBothPriorities();

            assertThat(bears.isTapped()).isFalse();
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target non-Aura enchantment")
    class DestroyNonAuraEnchantmentMode {

        @Test
        @DisplayName("Destroys the targeted non-Aura enchantment")
        void destroysNonAuraEnchantment() {
            harness.addToBattlefield(player2, new AngelicChorus());
            harness.setHand(player1, List.of(new EmeraldCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID chorusId = harness.getPermanentId(player2, "Angelic Chorus");
            harness.castInstant(player1, 0, 1, chorusId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Angelic Chorus");
            harness.assertInGraveyard(player2, "Angelic Chorus");
        }

        @Test
        @DisplayName("Cannot target an Aura")
        void cannotTargetAura() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent host = findPermanent(player2, "Grizzly Bears");
            Permanent aura = addAuraAttachedTo(player2, host);
            harness.addToBattlefield(player2, new AngelicChorus());
            harness.setHand(player1, List.of(new EmeraldCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, aura.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature loses flying until end of turn")
    class LoseFlyingMode {

        @Test
        @DisplayName("Target creature loses flying until end of turn")
        void losesFlyingUntilEndOfTurn() {
            Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
            harness.setHand(player1, List.of(new EmeraldCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();

            harness.castInstant(player1, 0, 2, elemental.getId());
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            harness.addToBattlefield(player2, new AngelicChorus());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new EmeraldCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID chorusId = harness.getPermanentId(player2, "Angelic Chorus");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, chorusId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private Permanent addAuraAttachedTo(Player player, Permanent host) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return aura;
    }
}
