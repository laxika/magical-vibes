package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "90")
public class GrafHarvest extends Card {

    public GrafHarvest() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.MENACE, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));
        addActivatedAbility(new ActivatedAbility(
                false, "{3}{B}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        CreateTokenEffect.blackZombie(1)
                ),
                "{3}{B}, Exile a creature card from your graveyard: Create a 2/2 black Zombie creature token."
        ));
    }
}
