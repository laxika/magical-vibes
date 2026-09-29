package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GlassCastHeart.class)
class GlassCastHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Blood token when a Vampire attacks")
    void createsBloodTokenWhenVampireAttacks() {
        addHeart();
        addVampire(player1);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    @DisplayName("Pays life and creates a lifelink Vampire token")
    void createsLifelinkVampireToken() {
        addHeart();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Vampire")
                        && permanent.getCard().getColors().size() == 2
                        && permanent.getCard().getColors().containsAll(List.of(CardColor.WHITE, CardColor.BLACK))
                        && permanent.getCard().getSubtypes().contains(CardSubtype.VAMPIRE)
                        && permanent.getCard().getKeywords().contains(Keyword.LIFELINK)
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1);
    }

    @Test
    @DisplayName("Sacrifices the Heart and thirteen Blood tokens to drain each opponent")
    void sacrificesHeartAndThirteenBloodTokens() {
        addHeart();
        for (int i = 0; i < 13; i++) {
            addBloodToken(player1);
        }
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(33);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
        assertThat(findPermanents(player1, "Glass-Cast Heart")).isEmpty();
        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    private Permanent addHeart() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new GlassCastHeart());
        heart.setSummoningSick(false);
        return heart;
    }

    private void addVampire(Player player) {
        Card vampire = new Card();
        vampire.setName("Test Vampire");
        vampire.setType(CardType.CREATURE);
        vampire.setColor(CardColor.BLACK);
        vampire.setSubtypes(List.of(CardSubtype.VAMPIRE));
        vampire.setPower(2);
        vampire.setToughness(2);
        Permanent permanent = new Permanent(vampire);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private void addBloodToken(Player player) {
        Card blood = new Card();
        blood.setName("Blood");
        blood.setType(CardType.ARTIFACT);
        blood.setSubtypes(List.of(CardSubtype.BLOOD));
        blood.setToken(true);
        gd.playerBattlefields.get(player.getId()).add(new Permanent(blood));
    }
}
