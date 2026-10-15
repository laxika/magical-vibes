package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LiberatedLivestock.class, HolyStrength.class, WrathOfGod.class})
class LiberatedLivestockTest extends BaseCardTest {

    @Test
    void deathCreatesCatBirdAndOxAndAttachesAuraFromHand() {
        HolyStrength aura = new HolyStrength();
        killLivestock(List.of(aura), List.of());

        assertTokenProfiles();
        chooseAura(aura);

        assertAttachedToToken();
        harness.assertNotInHand(player1, "Holy Strength");
    }

    @Test
    void canAttachAuraFromGraveyardToCreatedToken() {
        HolyStrength aura = new HolyStrength();
        killLivestock(List.of(), List.of(aura));

        assertTokenProfiles();
        chooseAura(aura);

        assertAttachedToToken();
        harness.assertNotInGraveyard(player1, "Holy Strength");
    }

    @Test
    void mayDeclineEachAuraAttachment() {
        HolyStrength aura = new HolyStrength();
        killLivestock(List.of(aura), List.of());

        assertTokenProfiles();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Holy Strength");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getName().equals("Holy Strength"));
    }

    private void killLivestock(List<Card> handAuras, List<Card> graveyard) {
        harness.addToBattlefield(player1, new LiberatedLivestock());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.setHand(player1, handAuras);
        harness.setGraveyard(player1, graveyard);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void assertTokenProfiles() {
        Permanent cat = findPermanent(player1, "Cat");
        Permanent bird = findPermanent(player1, "Bird");
        Permanent ox = findPermanent(player1, "Ox");

        assertThat(cat.getCard().getPower()).isEqualTo(1);
        assertThat(cat.getCard().getToughness()).isEqualTo(1);
        assertThat(cat.getCard().getKeywords()).contains(Keyword.LIFELINK);
        assertThat(bird.getCard().getPower()).isEqualTo(1);
        assertThat(bird.getCard().getToughness()).isEqualTo(1);
        assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(ox.getCard().getPower()).isEqualTo(2);
        assertThat(ox.getCard().getToughness()).isEqualTo(4);
    }

    private void chooseAura(Card aura) {
        PendingInteraction.AttachAurasChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.AttachAurasChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(aura.getId());
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
    }

    private void assertAttachedToToken() {
        Permanent attachedAura = findPermanent(player1, "Holy Strength");
        assertThat(attachedAura.getAttachedTo()).isIn(
                findPermanent(player1, "Cat").getId(),
                findPermanent(player1, "Bird").getId(),
                findPermanent(player1, "Ox").getId());
    }
}
