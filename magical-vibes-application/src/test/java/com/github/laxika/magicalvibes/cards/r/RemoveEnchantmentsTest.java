package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RemoveEnchantments.class, AuraOfSilence.class, GloriousAnthem.class, GrizzlyBears.class,
        HolyStrength.class, UnholyStrength.class})
class RemoveEnchantmentsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns qualifying enchantments and Auras, then destroys the other qualifying permanents")
    void returnsAndDestroysQualifyingPermanents() {
        Permanent ownCreature = addCreature(player1);
        Permanent opponentAttacker = addCreature(player2);
        opponentAttacker.setAttacking(true);
        opponentAttacker.setAttackTarget(player1.getId());
        Permanent opponentNonattacker = addCreature(player2);

        GloriousAnthem ownEnchantment = new GloriousAnthem();
        Permanent ownEnchantmentPermanent = harness.addToBattlefieldAndReturn(player1, ownEnchantment);

        AuraOfSilence opponentEnchantment = new AuraOfSilence();
        Permanent opponentEnchantmentPermanent = harness.addToBattlefieldAndReturn(player1, opponentEnchantment);
        markOwnedBy(opponentEnchantmentPermanent, player2);

        Permanent ownAuraOnOwnCreature = addAura(player1, new HolyStrength(), ownCreature);
        Permanent opponentAuraOnOwnCreature = addAura(player1, new UnholyStrength(), ownCreature);
        markOwnedBy(opponentAuraOnOwnCreature, player2);

        Permanent ownAuraOnOpponentAttacker = addAura(player2, new HolyStrength(), opponentAttacker);
        markOwnedBy(ownAuraOnOpponentAttacker, player1);
        Permanent opponentAuraOnOpponentAttacker = addAura(player2, new UnholyStrength(), opponentAttacker);

        Permanent ownAuraOnOpponentNonattacker = addAura(player2, new HolyStrength(), opponentNonattacker);
        markOwnedBy(ownAuraOnOpponentNonattacker, player1);

        harness.setHand(player1, List.of(new RemoveEnchantments()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(ownEnchantment, ownAuraOnOwnCreature.getCard(), ownAuraOnOpponentAttacker.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(ownEnchantmentPermanent, opponentEnchantmentPermanent,
                        ownAuraOnOwnCreature, opponentAuraOnOwnCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(ownAuraOnOpponentAttacker, opponentAuraOnOpponentAttacker)
                .contains(ownAuraOnOpponentNonattacker);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(opponentEnchantment, opponentAuraOnOwnCreature.getCard(), opponentAuraOnOpponentAttacker.getCard())
                .doesNotContain(ownEnchantment, (Card) ownAuraOnOwnCreature.getCard(),
                        (Card) ownAuraOnOpponentAttacker.getCard(), (Card) ownAuraOnOpponentNonattacker.getCard());
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private Permanent addAura(Player controller, Card aura, Permanent host) {
        Permanent auraPermanent = harness.addToBattlefieldAndReturn(controller, aura);
        auraPermanent.setAttachedTo(host.getId());
        return auraPermanent;
    }

    private void markOwnedBy(Permanent permanent, Player owner) {
        var controllerId = gd.playerBattlefields.entrySet().stream()
                .filter(entry -> entry.getValue().contains(permanent))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
        gd.stolenCreatures.put(permanent.getId(), owner.getId());
        if (!controllerId.equals(owner.getId())) {
            gd.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), "Test control effect", permanent.getId(), controllerId,
                    new GainControlOfTargetEffect(ControlDuration.PERMANENT), permanent.getId(),
                    null, null, EffectDuration.PERMANENT, 0));
        }
    }
}
