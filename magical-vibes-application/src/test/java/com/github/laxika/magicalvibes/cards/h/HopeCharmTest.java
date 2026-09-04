package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DarajaGriffin;
import com.github.laxika.magicalvibes.cards.m.MortalWound;
import com.github.laxika.magicalvibes.cards.r.RighteousAura;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HopeCharm.class, DarajaGriffin.class, MortalWound.class, RighteousAura.class})
class HopeCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Target creature gains first strike until end of turn")
    class FirstStrikeMode {

        @Test
        @DisplayName("Grants first strike to the targeted creature")
        void grantsFirstStrike() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new DarajaGriffin());
            harness.setHand(player1, List.of(new HopeCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 0, target.getId());
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        }

        @Test
        @DisplayName("First strike wears off at the cleanup step")
        void firstStrikeWearsOff() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new DarajaGriffin());
            harness.setHand(player1, List.of(new HopeCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 0, target.getId());
            harness.passBothPriorities();

            harness.passUntil(TurnStep.CLEANUP);

            assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        }

        @Test
        @DisplayName("Can target a creature controlled by an opponent")
        void canTargetOpponentCreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new DarajaGriffin());
            harness.setHand(player1, List.of(new HopeCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 0, target.getId());
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNonCreaturePermanent() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new RighteousAura());
            harness.setHand(player1, List.of(new HopeCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
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

            harness.assertLife(player2, before + 2);
        }
    }

    @Nested
    @DisplayName("Mode 2: Destroy target Aura")
    class DestroyAuraMode {

        @Test
        @DisplayName("Destroys the targeted Aura")
        void destroysAura() {
            Permanent host = harness.addToBattlefieldAndReturn(player2, new DarajaGriffin());
            Permanent aura = addAuraAttachedTo(player2, host);
            harness.setHand(player1, List.of(new HopeCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 2, aura.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);
            harness.assertInGraveyard(player2, "Mortal Wound");
        }

        @Test
        @DisplayName("Cannot target a non-Aura enchantment")
        void cannotTargetNonAuraEnchantment() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new RighteousAura());
            harness.setHand(player1, List.of(new HopeCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private Permanent addAuraAttachedTo(Player player, Permanent host) {
        Permanent aura = new Permanent(new MortalWound());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return aura;
    }
}
