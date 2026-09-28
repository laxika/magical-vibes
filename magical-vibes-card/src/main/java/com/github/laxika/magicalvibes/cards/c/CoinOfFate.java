package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesOneOfActivatedExiledCreatureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "15")
@CardRegistration(set = "FIC", collectorNumber = "104")
public class CoinOfFate extends Card {

    public CoinOfFate() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{W}",
                List.of(
                        new ExileNCardsFromGraveyardCost(2, CardType.CREATURE, null, false, true),
                        new SacrificeSelfCost(),
                        new OpponentChoosesOneOfActivatedExiledCreatureCardsEffect(),
                        new BecomeMonarchEffect()),
                "{3}{W}, {T}, Exile two creature cards from your graveyard, Sacrifice this artifact: "
                        + "An opponent chooses one of the exiled cards. You put that card on the bottom "
                        + "of your library and return the other to the battlefield tapped. You become the monarch."));
    }
}
