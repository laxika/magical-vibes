package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "154")
public class MoratoriumStone extends Card {

    public MoratoriumStone() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD)),
                "{2}, {T}: Exile target card from a graveyard."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}{B}",
                List.of(new SacrificeSelfCost(), new ExileTargetGraveyardCardAndAllWithSameNameEffect()),
                "{2}{W}{B}, {T}, Sacrifice this artifact: Exile target nonland card from a graveyard, all other cards from graveyards with the same name as that card, and all permanents with that name."
        ));
    }
}
