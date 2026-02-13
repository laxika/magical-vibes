package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardToBattlefieldEffect;

import java.util.List;

public class NomadMythmaker extends Card {

    public NomadMythmaker() {
        super("Nomad Mythmaker", CardType.CREATURE, "{2}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.NOMAD, CardSubtype.CLERIC));
        setCardText("{W}, {T}: Put target Aura card from a graveyard onto the battlefield under your control attached to a creature you control.");
        setPower(2);
        setToughness(2);
        addActivatedAbility(new ActivatedAbility(true, "{W}", List.of(new ReturnAuraFromGraveyardToBattlefieldEffect()), false, "{W}, {T}: Put target Aura card from a graveyard onto the battlefield under your control attached to a creature you control."));
    }
}
