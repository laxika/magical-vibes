package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterworkOfIngenuity.class, Bonesplitter.class, GrizzlyBears.class})
class MasterworkOfIngenuityTest extends BaseCardTest {

    @Test
    @DisplayName("May enter as a copy of an Equipment and retains its equip ability")
    void copiesEquipmentAndCanEquip() {
        Permanent bonesplitter = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        MasterworkOfIngenuity source = new MasterworkOfIngenuity();
        castMasterwork(source);

        chooseCopy(bonesplitter);

        Permanent copy = findCopy(source);
        assertThat(copy.getCard().getName()).isEqualTo("Bonesplitter");
        assertThat(copy.getCard().getActivatedAbilities()).hasSize(1);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(copy), null, creature.getId());
        harness.passBothPriorities();

        assertThat(copy.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining to copy leaves Masterwork of Ingenuity without an equip ability")
    void declinesToCopy() {
        harness.addToBattlefield(player2, new Bonesplitter());
        MasterworkOfIngenuity source = new MasterworkOfIngenuity();
        castMasterwork(source);

        harness.handleMayAbilityChosen(player1, false);

        Permanent entered = findCopy(source);
        assertThat(entered.getCard().getName()).isEqualTo("Masterwork of Ingenuity");
        assertThat(entered.getCard().getActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Does not offer to copy when no Equipment is on the battlefield")
    void doesNotOfferCopyChoiceWithoutEquipment() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        MasterworkOfIngenuity source = new MasterworkOfIngenuity();
        castMasterwork(source);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findCopy(source).getCard().getName()).isEqualTo("Masterwork of Ingenuity");
    }

    private void castMasterwork(MasterworkOfIngenuity source) {
        harness.setHand(player1, List.of(source));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseCopy(Permanent target) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
    }

    private Permanent findCopy(Card source) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(source.getId()))
                .findFirst()
                .orElseThrow();
    }
}
