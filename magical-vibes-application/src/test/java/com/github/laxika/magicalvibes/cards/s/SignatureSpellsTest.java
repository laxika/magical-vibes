package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SignatureSpells.class, Cancel.class, CounselOfTheSoratami.class, Shock.class})
class SignatureSpellsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by seeking two exact-mana-value-three spells into source-tracked exile")
    void seeksTwoEligibleSpellsIntoExile() {
        Cancel cancel = new Cancel();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(cancel, counsel, shock));
        harness.setHand(player1, List.of(new SignatureSpells()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Signature Spells");
        assertThat(gd.getCardsExiledByPermanent(source.getId()))
                .containsExactlyInAnyOrder(cancel, counsel);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Upkeep can copy and cast one spell exiled with Signature Spells for free")
    void upkeepCopiesAndCastsExiledSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.addToBattlefield(player1, new SignatureSpells());
        Permanent source = findPermanent(player1, "Signature Spells");
        gd.exiledCards.add(new ExiledCardEntry(counsel, player1.getId(), source.getId()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Counsel of the Soratami")
                && entry.isCopy());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(counsel);
    }
}
