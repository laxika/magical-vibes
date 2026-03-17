package com.github.laxika.magicalvibes.model.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GraveyardChoiceState {

    private UUID playerId;
    private Set<Integer> validIndices;
    private GraveyardChoiceDestination destination;
    private List<Card> cardPool;
    private boolean gainLifeEqualToManaValue;
    private UUID attachToSourcePermanentId;
    private CardColor grantColor;
    private CardSubtype grantSubtype;
    private int exileRemainingCount;
    private int gainLifeIfCreatureAmount;
    private UUID gainLifeIfCreaturePlayerId;

    public GraveyardChoiceState() {
    }

    public GraveyardChoiceState(UUID playerId, Set<Integer> validIndices,
                                GraveyardChoiceDestination destination, List<Card> cardPool) {
        this.playerId = playerId;
        this.validIndices = validIndices;
        this.destination = destination;
        this.cardPool = cardPool;
    }

    public UUID playerId() {
        return playerId;
    }

    public Set<Integer> validIndices() {
        return validIndices;
    }

    public GraveyardChoiceDestination destination() {
        return destination;
    }

    public void setDestination(GraveyardChoiceDestination destination) {
        this.destination = destination;
    }

    public List<Card> cardPool() {
        return cardPool;
    }

    public void setCardPool(List<Card> cardPool) {
        this.cardPool = cardPool;
    }

    public boolean gainLifeEqualToManaValue() {
        return gainLifeEqualToManaValue;
    }

    public void setGainLifeEqualToManaValue(boolean value) {
        this.gainLifeEqualToManaValue = value;
    }

    public UUID attachToSourcePermanentId() {
        return attachToSourcePermanentId;
    }

    public void setAttachToSourcePermanentId(UUID permanentId) {
        this.attachToSourcePermanentId = permanentId;
    }

    public CardColor grantColor() {
        return grantColor;
    }

    public void setGrantColor(CardColor grantColor) {
        this.grantColor = grantColor;
    }

    public CardSubtype grantSubtype() {
        return grantSubtype;
    }

    public void setGrantSubtype(CardSubtype grantSubtype) {
        this.grantSubtype = grantSubtype;
    }

    public int exileRemainingCount() {
        return exileRemainingCount;
    }

    public void setExileRemainingCount(int exileRemainingCount) {
        this.exileRemainingCount = exileRemainingCount;
    }

    public int gainLifeIfCreatureAmount() {
        return gainLifeIfCreatureAmount;
    }

    public void setGainLifeIfCreatureAmount(int gainLifeIfCreatureAmount) {
        this.gainLifeIfCreatureAmount = gainLifeIfCreatureAmount;
    }

    public UUID gainLifeIfCreaturePlayerId() {
        return gainLifeIfCreaturePlayerId;
    }

    public void setGainLifeIfCreaturePlayerId(UUID gainLifeIfCreaturePlayerId) {
        this.gainLifeIfCreaturePlayerId = gainLifeIfCreaturePlayerId;
    }

    public GraveyardChoiceState deepCopy() {
        GraveyardChoiceState copy = new GraveyardChoiceState(
                playerId,
                validIndices != null ? new HashSet<>(validIndices) : null,
                destination,
                cardPool != null ? new ArrayList<>(cardPool) : null
        );
        copy.gainLifeEqualToManaValue = this.gainLifeEqualToManaValue;
        copy.attachToSourcePermanentId = this.attachToSourcePermanentId;
        copy.grantColor = this.grantColor;
        copy.grantSubtype = this.grantSubtype;
        copy.exileRemainingCount = this.exileRemainingCount;
        copy.gainLifeIfCreatureAmount = this.gainLifeIfCreatureAmount;
        copy.gainLifeIfCreaturePlayerId = this.gainLifeIfCreaturePlayerId;
        return copy;
    }
}
