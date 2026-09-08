package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardCreateTokenIfCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "170")
public class EtherealAbsolution extends Card {

    public EtherealAbsolution() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.OPPONENT_CREATURES));

        CreateTokenEffect spirit = new CreateTokenEffect(
                1, "Spirit", 1, 1, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.SPIRIT),
                Set.of(Keyword.FLYING), Set.of());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{B}",
                List.of(new ExileGraveyardCardCreateTokenIfCreatureEffect(
                        GraveyardSearchScope.OPPONENT_GRAVEYARD, spirit)),
                "{2}{W}{B}: Exile target card from an opponent's graveyard. If it was a creature card, "
                        + "you create a 1/1 white and black Spirit creature token with flying."
        ));
    }
}
