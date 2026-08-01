package com.github.laxika.magicalvibes.cards.h;

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

class HopeCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Target creature gains first strike until end of turn")
    class FirstStrikeMode {

        @Test
        @DisplayName("Grants first strike to the targeted creature")
        void grantsFirstStrike() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new HopeCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.FIRST_STRIKE)).isTrue();
        }

        @Test
        @DisplayName("First strike wears off at the cleanup step")
        void firstStrikeWearsOff() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new HopeCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.FIRST_STRIKE)).isFalse();
        }
    }

    @Nested
    @DisplayName("Mode 1: Target player gains 2 life")
    class GainLifeMode {

        @Test
        @DisplayName("Target player gains 2 life")
        void targetPlayerGainsLife() {
            harness.setHand(player1, List.of(new HopeCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            int before = gd.playerLifeTotals.get(player2.getId());

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(before + 2);
        }
    }

    @Nested
    @DisplayName("Mode 2: Destroy target Aura")
    class DestroyAuraMode {

        @Test
        @DisplayName("Destroys the targeted Aura")
        void destroysAura() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent host = findPermanent(player2, "Grizzly Bears");
            Permanent aura = addAuraAttachedTo(player2, host);
            harness.setHand(player1, List.of(new HopeCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 2, aura.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);
            harness.assertInGraveyard(player2, "Pacifism");
        }

        @Test
        @DisplayName("Cannot target a non-Aura enchantment")
        void cannotTargetNonAuraEnchantment() {
            harness.addToBattlefield(player2, new AngelicChorus());
            harness.setHand(player1, List.of(new HopeCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

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
