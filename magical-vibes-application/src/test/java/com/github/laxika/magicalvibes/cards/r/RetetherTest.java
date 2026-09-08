package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.t.TattooWard;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Retether.class, GrizzlyBears.class, HolyStrength.class, TattooWard.class, WildGrowth.class})
class RetetherTest extends BaseCardTest {

    @Test
    @DisplayName("Returns each Aura attached to a creature")
    void returnsEachAuraAttachedToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(aura));
        castRetether();

        Permanent returnedAura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(aura.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returnedAura.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Lets the controller choose among legal creature attachments")
    void letsControllerChooseAmongLegalCreatureAttachments() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(aura));

        castRetether();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstCreature.getId(), chosenCreature.getId());

        harness.handlePermanentChosen(player1, chosenCreature.getId());

        Permanent returnedAura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(aura.getId()))
                .findFirst().orElseThrow();
        assertThat(returnedAura.getAttachedTo()).isEqualTo(chosenCreature.getId());
    }

    @Test
    @DisplayName("Leaves an Aura in the graveyard when no creature can be enchanted")
    void leavesAuraWhenNoCreatureCanBeEnchanted() {
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(aura));
        castRetether();

        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Checks attachment legality before returning all Auras")
    void checksAttachmentLegalityBeforeReturningAllAuras() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card tattooWard = new TattooWard();
        Card holyStrength = new HolyStrength();
        Card wildGrowth = new WildGrowth();
        harness.setGraveyard(player1, List.of(tattooWard, holyStrength, wildGrowth));
        castRetether();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(tattooWard.getId())
                        && permanent.getAttachedTo().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertInGraveyard(player1, "Wild Growth");
    }

    private void castRetether() {
        harness.setHand(player1, List.of(new Retether()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
