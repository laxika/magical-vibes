package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunkenPalace.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class})
class SunkenPalaceTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new SunkenPalace()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Sunken Palace").isTapped()).isTrue();
    }

    @Test
    void ordinaryManaDoesNotCopyASpell() {
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(findPermanent(player1, "Sunken Palace").isTapped()).isTrue();
    }

    @Test
    void cannotActivateCopyManaAbilityWithOnlySixGraveyardCards() {
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.setGraveyard(player1, graveyardCards().subList(0, 6));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Sunken Palace").isTapped()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    void freeSpellDoesNotConsumeTheCopyRider() {
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.setGraveyard(player1, graveyardCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new FountainOfYouth(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Fountain of Youth")).isEqualTo(1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
    }

    @Test
    void activatingAnotherManaAbilityDoesNotConsumeTheCopyRider() {
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setGraveyard(player1, graveyardCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.activateAbility(player1, 2, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    void spellPaidWithOtherManaDoesNotTriggerACopy() {
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.setGraveyard(player1, graveyardCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    void copiesPermanentSpellPaidWithItsMana() {
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.setGraveyard(player1, graveyardCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(7);
    }

    @Test
    void copiesActivatedAbilityPaidWithItsMana() {
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setGraveyard(player1, graveyardCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(7);
    }

    private List<Card> graveyardCards() {
        return List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
    }
}
